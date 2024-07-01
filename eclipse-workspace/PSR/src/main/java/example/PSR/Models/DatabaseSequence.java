package example.PSR.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "database_sequences")
@Data
public class DatabaseSequence {

    @Id
    private String Id;

    private long seq;

    public DatabaseSequence() {}

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}

