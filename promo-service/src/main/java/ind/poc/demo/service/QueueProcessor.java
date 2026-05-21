package ind.poc.demo.service;

import ind.poc.demo.task.InventoryTask;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.input.ObservableInputStream;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Log4j2
public class QueueProcessor implements CommandLineRunner, DisposableBean {

    private final ArrayBlockingQueue<InventoryTask> dataUpdatePool;
    private final Executor databaseAsyncExecutor;
    // 提升為成員變數，用來控管消費流的生命週期
    private Disposable disposableConsumer;

    @Override
    public void run(String... args) throws Exception {
        final Observable<InventoryTask> updatedTaskObserser = Observable.create(emitter -> {
            try {
                // 當訂閱沒有被取消時，持續從 Queue 中拿資料
                while (!emitter.isDisposed()) {
                    // take() 在 Queue 為空時會自動阻塞（等待），不吃 CPU，直到有新資料進來
                    InventoryTask task = dataUpdatePool.take();
                    // 【優化點 2】：被喚醒後，發送前再次確認下游是否還在聆聽（防範阻塞期間訂閱被取消）
                    if (!emitter.isDisposed()) {
                        emitter.onNext(task);
                    } else {
                        // 如果下游已經取消訂閱，且我們剛好 take() 出一筆資料
                        // 為了防止這筆資料在記憶體中憑空消失（漏單），應該在這裡將它回滾
                        log.warn("消費端已關閉，對剛取出但未處理的任務執行安全回滾");
                        if (task.getRollback() != null) {
                            task.getRollback().accept(null);
                        }
                        break; // 跳出迴圈
                    }
                }
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            } catch (InterruptedException e) {
                // 如果執行緒被中斷，通知 RxJava 結束
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
                Thread.currentThread().interrupt();
            }
        });

        this.disposableConsumer = updatedTaskObserser
                .observeOn(Schedulers.from(databaseAsyncExecutor))
                .concatMap(task -> {
                    // 將每個單獨的 Supplier<Integer> 轉換成一個全新的 Observable/Single
                    return Observable.fromCallable(() -> task.execute())
                            .subscribeOn(Schedulers.from(databaseAsyncExecutor))
                            .onErrorReturn(throwable -> {
                                log.error("非同步資料庫庫存更新失敗，觸發任務內部 Rollback", throwable);
                                task.getRollback().accept(null);
                                return -1;
                            }); // 確保任務執行在後台
                })
                .subscribe(
                        result -> {
                            System.out.println("任務處理完成，結果: " + result);
                        },
                        throwable -> {
                            throwable.printStackTrace();
                        }
                );

    }

    @Override
    public void destroy() throws Exception {
        log.info("開始執行 QueueProcessor 的優雅停機資源清理...");

        // 1. 先確認隊列裡是否還有殘留任務，給予短暫時間消化
        int retryCount = 0;
        while (!dataUpdatePool.isEmpty() && retryCount < 30) {
            log.info("發現資料庫非同步佇列中仍有 {} 筆任務，等待消費端消化中...", dataUpdatePool.size());
            try {
                TimeUnit.MILLISECONDS.sleep(200); // 每次等 0.2 秒，最多等 6 秒
            } catch (InterruptedException e) {
                log.warn("優雅停機等待期間遭遇執行緒中斷，提前結束等待迴圈。");
                Thread.currentThread().interrupt(); // 恢復中斷標記
                break;
            }
            retryCount++;
        }

        if (!dataUpdatePool.isEmpty()) {
            log.warn("等待超時！佇列中仍殘留 {} 筆任務，將強制執行清空並回滾庫存，防止漏單！", dataUpdatePool.size());
            // 如果真的等太久（例如資料庫完全卡死），強制把剩下拿出來全部回滾，還給 Redis
            InventoryTask<?> remainingTask;
            while ((remainingTask = dataUpdatePool.poll()) != null) {
                if (remainingTask.getRollback() != null) {
                    remainingTask.getRollback().accept(null);
                }
            }
        }

        // 2. 確定隊列空了或處理完了，正式切斷 RxJava 的消費流通道
        if (disposableConsumer != null && !disposableConsumer.isDisposed()) {
            log.info("正在切斷 RxJava 消費流通道...");
            disposableConsumer.dispose();
        }

        log.info("QueueProcessor 資源清理完成，允許 Spring 銷毀後續的資料庫連線池。");
    }
}
