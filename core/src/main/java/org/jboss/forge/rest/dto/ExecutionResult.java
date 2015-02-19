package org.jboss.forge.rest.dto;

public class ExecutionResult {
	private final ExecutionStatus status;
	private final String message;
	private final String output;
	private final String err;
    private final String detail;

    public ExecutionResult(ExecutionStatus status, String message, String output, String err, String detail) {
        this.status = status;
        this.message = message;
        this.output = output;
        this.err = err;
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", output='" + output + '\'' +
                ", err='" + err + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }

    public String getDetail() {
        return detail;
    }

    public String getErr() {
        return err;
    }

    public String getMessage() {
        return message;
    }

    public String getOutput() {
        return output;
    }

    public ExecutionStatus getStatus() {
        return status;
    }
}
