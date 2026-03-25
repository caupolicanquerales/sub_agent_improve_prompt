package com.capo.sub_agent_improve_prompt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.capo.sub_agent_improve_prompt.request.GenerationSyntheticDataRequest;
import com.capo.sub_agent_improve_prompt.response.DataMessage;
import com.capo.sub_agent_improve_prompt.utils.ConverterUtil;

import reactor.core.publisher.Flux;

@Service
public class ExecutingAgentService {

	private static final Logger log = LoggerFactory.getLogger(ExecutingAgentService.class);

	private final ChatClient chatClient;
	private final String systemPrompt;
	
	@Value(value="${event.name-chat}")
	private String eventName;
	
	public ExecutingAgentService(@Qualifier("chatClientImprovement") ChatClient chatClient,
			@Qualifier("systemPrompt") String systemPrompt) {
		this.chatClient = chatClient;
		this.systemPrompt= systemPrompt;
	}
	
	public Flux<ServerSentEvent<DataMessage>> executing(GenerationSyntheticDataRequest request) {

	    return chatClient.prompt()
	    		.messages(new SystemMessage(systemPrompt))
	    		.user(request.getPrompt())
                .stream()
                .chatResponse()
                .map(this::getTokenMessage)
                .map(ConverterUtil::setDataMessage)
                .map(data -> ConverterUtil.setServerSentEvent(data, eventName))
                .doOnComplete(() -> log.info("AI Stream Finished. Sending completion flag..."))
        	    .concatWith(Flux.defer(() -> {
        	        DataMessage finalMsg = ConverterUtil.setDataMessage(eventName + "-COMPLETED");
        	        return Flux.just(ConverterUtil.setServerSentEvent(finalMsg, eventName));
        	    }))
        	    .doOnTerminate(() -> log.info("HTTP Response fully closed on server"))
        	    .onErrorResume(WebClientResponseException.class, e -> {
        	        String errorBody = e.getResponseBodyAsString();
        	        log.error("OpenAI 400 Error Body: {}", e);
        	        return Flux.error(new RuntimeException("OpenAI API call failed: " + errorBody, e));
        	    });
	}
	
	private String getTokenMessage(ChatResponse chatResponse) {
	    if (chatResponse == null || chatResponse.getResult() == null) {
	        return "The sub-agent was unable to process the request.";
	    }
	    String content = chatResponse.getResult().getOutput().getText();
	    return (content != null && !content.isEmpty()) ? content : "Error: Sub-agent could not generate a response.";
	}
}
