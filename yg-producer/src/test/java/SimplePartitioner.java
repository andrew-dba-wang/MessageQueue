/**
 * Created by root on 16-9-28.
 */

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class SimplePartitioner implements Partitioner {
    public SimplePartitioner(VerifiableProperties props) {

    }

    public int partition(Object key, int numPartitions) {
        int partition = 0;
        String keyword=key.toString();
        int offset = keyword.lastIndexOf('.');
        if (offset > 0) {
            partition = Integer.parseInt(keyword.substring(offset + 1)) % numPartitions;
        }
        return partition;
    }
}
