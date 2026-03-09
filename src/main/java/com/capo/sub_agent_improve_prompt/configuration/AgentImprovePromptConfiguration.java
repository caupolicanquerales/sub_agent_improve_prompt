package com.capo.sub_agent_improve_prompt.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AgentImprovePromptConfiguration {

	@Bean
    public ChatClient chatClientImprovement(ChatClient.Builder builder) {
        return builder
    		.clone()
    		.defaultTools()
    		.defaultToolNames()
        	.defaultAdvisors()
        	.defaultSystem(systemPrompt)
            .build();
    }
	
	private String systemPrompt = """
		    You are a Senior UI/UX Prompt Engineer. Your goal is to refactor and enhance user-provided UI descriptions into high-fidelity prompts for gpt-image-1.

			INSTRUCTIONS:
			1. ENHANCE VISUAL FIDELITY: Add descriptors for digital surfaces (e.g., "frosted glass," "brushed metal," "matte plastic," "vibrant OLED colors").
			2. DEFINE LIGHTING: Use UI-specific lighting terms like "soft global illumination," "subtle drop shadows," "rim lighting on buttons," or "backlit elements."
			3. FIX COMPOSITION: Ensure the layout is described as a "clean front-facing screenshot" or "isometric 3D web view" to avoid warped perspectives.
			4. SPECIFY STYLE: Apply modern design trends (e.g., "Apple-inspired minimalism," "Material Design 3," "SaaS dashboard aesthetic," "high-end Fintech UI").
			5. TECHNICAL POLISH: Append quality tokens like "8k, clean typography, pixel-perfect, sharp edges, professional color grading."
			
			OUTPUT:
			Output ONLY the final improved prompt. No conversational filler.
		    """;
}
