package commands;

import compiler.models.contract.ContractCompiled;
import compiler.models.contract.ContractToCompile;
import compiler.models.dto.compilecontract.CompileContractRequest;
import compiler.models.dto.compilecontract.CompileContractResponse;
import compiler.module.CompilerModule;
import lcp.lib.communication.module.channel.ChannelMessage;
import lcp.lib.communication.module.channel.ChannelMessagePayload;
import lcp.lib.communication.module.channel.responses.RequestNotFound;
import lcp.lib.datastructures.Triple;
import lcp.lib.dfa.states.DfaState;
import lcp.lib.dfa.transitions.TransitionData;
import lombok.AllArgsConstructor;
import modules.StipulaCompilerModule;
import utils.FileUtils;

import java.io.File;

@AllArgsConstructor
public class CompileCommand implements Command {
    private final String inputFilePath;
    private final String outputFolderPath;
    private final StipulaCompilerModule stipulaCompiler;
    private static final String bytecodeHeader = "========================\n\t\tBYTECODE\n========================\n\n";
    private static final String stateMachineHeader = "\n========================\n\tSTATE MACHINE\n========================\n\n";

    @Override
    public void execute() {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            // TODO: error
        }

        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists() || !outputFolder.isDirectory()) {
            // TODO: error
        }

        String[] structureInputFilePath = inputFilePath.split("\\.");
        if (structureInputFilePath.length < 2) {
            // TODO: error
        } else {
            if (structureInputFilePath[0].trim().isEmpty()) {
                // TODO: error
            }

            if (!structureInputFilePath[structureInputFilePath.length - 1].equals("stipula")) {
                // TODO: error
            }
        }

        String inputFileContent = FileUtils.read(inputFilePath);
        if (inputFileContent.trim().isEmpty()) {
            // TODO: error
        }

        String fileName = inputFile.getName();
        String inputFileNameWithoutExtension = "";
        int lastIndex = fileName.lastIndexOf('.');
        inputFileNameWithoutExtension = fileName.substring(0, lastIndex);
        String outputFileName = inputFileNameWithoutExtension + ".sb";

        ContractToCompile contractToCompile = new ContractToCompile(inputFileContent);
        ChannelMessage message = stipulaCompiler.sendAndReceive(CompilerModule.class.getSimpleName(), new CompileContractRequest(contractToCompile));
        ChannelMessagePayload payload = message.getPayload();

        if (payload instanceof CompileContractResponse) {
            ContractCompiled contractCompiled = ((CompileContractResponse) payload).getContractCompiled();
            String bytecode = contractCompiled.getBytecode();

            String initialState = "Initial state: " + contractCompiled.getInitialState().getName() + "\n";

            StringBuilder finalStates = new StringBuilder("Final states: \n");
            finalStates.append("\tAcceptance states: ");
            for (DfaState finalState : contractCompiled.getFinalStates().getAcceptanceStates()) {
                finalStates.append(finalState.getName()).append(",");
            }

            finalStates.append("\n\tFailing states: ");
            for (DfaState finalState : contractCompiled.getFinalStates().getFailingStates()) {
                finalStates.append(finalState.getName()).append(",");
            }

            StringBuilder transitions = new StringBuilder("Transitions: \n");
            for (Triple<DfaState, DfaState, TransitionData> transition : contractCompiled.getTransitions()) {
                transitions.append("\t").append(transition.getFirst().getName()).append(" -> ").append(transition.getSecond().getName()).append(", transition data: ").append(transition.getThird().toString()).append("\n");
            }

            String content = bytecodeHeader + bytecode + stateMachineHeader + initialState + finalStates + "\n" + transitions;

            boolean result = FileUtils.write(outputFolderPath + outputFileName, content);
            if (!result) {
                // TODO
            }
        } else if (payload instanceof RequestNotFound) {
            // TODO
        } else {
            // TODO
        }
    }
}
