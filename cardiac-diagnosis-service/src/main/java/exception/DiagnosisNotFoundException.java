package exception;

public class DiagnosisNotFoundException
        extends RuntimeException {

    public DiagnosisNotFoundException(String id) {

        super("Diagnosis not found with id: " + id);
    }
}