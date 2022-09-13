package net.corda.cordapp.java.jdk11.helloworld.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.cordapp.java.jdk11.helloworld.contracts.HelloWorldContract;
import net.corda.cordapp.java.jdk11.helloworld.states.HelloWorldState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HelloWorldFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class HelloWorldFlowInitiator extends FlowLogic<SignedTransaction>{

        //private variables
        private Party sender ;
        private Party receiver;

        //public constructor
        public HelloWorldFlowInitiator(Party receiver) {
            this.receiver = receiver;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            //Hello World message
            var msg = "Hello-World";
            this.sender = getOurIdentity();

            // Step 1. Get a reference to the notary service on our network and our key pair.
            /** Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)*/
            final var notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary Service,L=Zurich,C=CH"));

            //Compose the State that carries the Hello World message
            final var output = new HelloWorldState(msg,sender,receiver);

            // Step 3. Create a new TransactionBuilder object.
            final var builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addOutputState(output);
            builder.addCommand(new HelloWorldContract.Commands.Send(), Arrays.asList(this.sender.getOwningKey(),this.receiver.getOwningKey()) );


            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final var ptx = getServiceHub().signInitialTransaction(builder);


            // Step 6. Collect the other party's signature using the SignTransactionFlow.
            final var otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
            otherParties.remove(getOurIdentity());
            final var sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

            var stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

            // Step 7. Assuming no exceptions, we can now finalise the transaction
            return subFlow(new FinalityFlow(stx, sessions));
        }
    }

    @InitiatedBy(HelloWorldFlowInitiator.class)
    public static class HelloWorldFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public HelloWorldFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}
