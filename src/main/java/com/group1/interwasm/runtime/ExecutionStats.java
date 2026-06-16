package com.group1.interwasm.runtime;

public final class ExecutionStats {
    /** One per execute() call -> the cost of dispatch. */
    public long dispatches = 0;

    /** Stack pops + local reads -> the cost of Operandenzugriff (reads). */
    public long operandReads = 0;

    /** Stack pushes + local writes -> the cost of Operandenzugriff (writes). */
    public long operandWrites = 0;

    public long totalOperandOps() {
        return operandReads + operandWrites;
    }

    public void reset() {
        dispatches = 0;
        operandReads = 0;
        operandWrites = 0;
    }

    @Override
    public String toString() {
        return String.format(
                "dispatches=%d  operandReads=%d  operandWrites=%d  totalOperandOps=%d  reads/dispatch=%.2f",
                dispatches, operandReads, operandWrites, totalOperandOps(),
                dispatches == 0 ? 0.0 : (double) totalOperandOps() / dispatches);
    }
}
