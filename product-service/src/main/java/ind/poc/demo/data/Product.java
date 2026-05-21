package ind.poc.demo.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Products") // Replace with your actual table name if different
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @Column(name = "Id", length = 36, nullable = false)
    private String id;

    @Column(name = "Name", nullable = false, length = 36)
    private String name;

    @Column(name = "Description", nullable = false, length = 100)
    private String description;
}



