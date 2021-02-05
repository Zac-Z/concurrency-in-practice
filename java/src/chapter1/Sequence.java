package chapter1;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * @author 138049
 */
@ThreadSafe
public class Sequence {
    @GuardedBy("this")
    private int value;

    public synchronized int getNext() {
        return value++;
    }
}
