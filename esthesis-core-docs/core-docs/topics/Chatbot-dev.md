# Chatbot

We include a Chatbot functionality as a Technology Preview Feature.

Our Chatbot makes use of
Quarkus' [langchain4j integration](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html)
and is compatible with [OpenAI API](https://platform.openai.com) as well as a custom
[Ollama](https://github.com/ollama/ollama) server.

## Configuration

Our esthesis dependencies Helm Chart includes an optional Ollama server deployment. You can enable
it by specifying `--set charts_enabled.ollama=true` during deployment of the Helm Chart. Our default
Ollama deployment uses [qwen3:0.6b](https://huggingface.co/Qwen/Qwen3-0.6B) as a chat model, and
[nomic-embed-text](https://ollama.com/library/nomic-embed-text) as an embedding model.
The `application-dev.yaml` file of the Chatbot service comes already configured to use the above
Ollama deployment and models.

<warning>
	Running an Ollama server in your Kubernetes cluster with no GPU or AI-features available can be
	very slow. In such cases, expect response times of several seconds, or even the Chatbot service
	intermittently crashing due to timeouts. We recommend using Ollama with a GPU-enabled node pool,
	or alternatively, using the OpenAI integration.
</warning>

### Switching to OpenAI
To use the OpenAI API you need to first obtain an API key from OpenIA. This incurs a cost, as access
to the OpenAI API is not free. Once you have the API key, you can enable OpenAI integration for the
Chatbot as:
1. Create a `local-env.sh` file in the same level as `dev-chatbot.sh`.
2. Add the following line to the `local-env.sh` file:
	```bash
	export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY="sk-proj-..."
 	export QUARKUS_LANGCHAIN4J_CHAT_MODEL_PROVIDER="openai"
 	export QUARKUS_LANGCHAIN4J_EMBEDDING_MODEL_PROVIDER="openai"
	```
