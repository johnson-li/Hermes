package operation;

public abstract class Operation {

    public String getOperationName() {
        return getClass().getSimpleName();
    }
}
