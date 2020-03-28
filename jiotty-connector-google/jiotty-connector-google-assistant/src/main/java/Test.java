import com.google.assistant.embedded.v1alpha2.AssistConfig;
import com.google.assistant.embedded.v1alpha2.AssistRequest;
import com.google.assistant.embedded.v1alpha2.EmbeddedAssistantGrpc;

final class Test {
    public static void main(String[] args) {
        EmbeddedAssistantGrpc.newStub().assist().onNext(AssistRequest.newBuilder()
                .setConfig(AssistConfig.newBuilder()
                        .setTextQuery("Thermostat off"))
                .build());
    }
}
