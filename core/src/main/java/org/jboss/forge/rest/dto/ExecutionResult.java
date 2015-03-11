package org.jboss.forge.rest.dto;

public class ExecutionResult {
	private final ExecutionStatus status;
	private final String message;
	private final String output;
	private final String err;
    private final String detail;
    private WizardResultsDTO wizardResults;
    private boolean canMoveToNextStep;

    public ExecutionResult(ExecutionStatus status, String message, String output, String err, String detail, boolean canMoveToNextStep) {
        this.status = status;
        this.message = message;
        this.output = output;
        this.err = err;
        this.detail = detail;
        this.canMoveToNextStep = canMoveToNextStep;
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

    public void setWizardResults(WizardResultsDTO wizardResults) {
        this.wizardResults = wizardResults;
    }

    public WizardResultsDTO getWizardResults() {
        return wizardResults;
    }

    public boolean isCanMoveToNextStep() {
        return canMoveToNextStep;
    }

    /**
     * Returns true if the command completed successfully and its either not a wizard command or it is a wizard and the last page was completed
     */
    public boolean isCommandCompleted() {
        return status.equals(ExecutionStatus.SUCCESS) && (wizardResults == null || !isCanMoveToNextStep());
    }
}
