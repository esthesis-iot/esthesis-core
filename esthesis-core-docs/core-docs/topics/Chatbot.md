# Chatbot

esthesis comes with a Chatbot functionality, currently provided as a **Technology Preview Feature**.
As a Technology Preview Feature, it is not yet fully supported and may be subject to change in future releases.

Chatbot functionality is **disabled by default**.

---

## LLM support
The Chatbot can integrate with **OpenAI API** or **Ollama**.
Depending on your preference, follow one of the setup options below.

## Default Configuration

```yaml
chatbot:
  service:
    enabled: false
  chatModel:
    provider: "ollama"
  embeddingModel:
    provider: "ollama"
  easyRag:
    path: "rag"
    pathType: "classpath"
  openai:
    apiKey: "change-me"
    chatModel:
      modelName: "gpt-3.5-turbo"
      temperature: 0.1
  ollama:
    baseUrl: "http://ollama:11434"
    timeout: "60s"
    chatModel:
      modelId: "qwen3:0.6b"
      temperature: 0.1
    embeddingModel:
      modelId: "nomic-embed-text"
      temperature: 0.1
   ```
### Using Ollama (Local Models)
1. Deploy the Ollama server in your Kubernetes cluster using the esthesis dependencies Helm Chart.
	 You can enable it by setting `--set charts_enabled.ollama=true` during `esthesis-core-deps` deployment.
2. Ensure Ollama is running and accessible to your pods.
3. Set `chatbot.service.enabled` to `true`.
4. Set `chatbot.chatModel.provider` and `embeddingModel.provider` to `ollama`.
5. Tweak other configuration options as needed, such as:
   - `ollama.baseUrl`
   - `ollama.chatModel.modelId`
   - `ollama.embeddingModel.modelId`

**Example configuration:**
```yaml
chatbot:
	service:
		enabled: true
	chatModel:
		provider: "ollama"
	embeddingModel:
		provider: "ollama"
	ollama:
		baseUrl: "http://ollama:11434"
		chatModel:
			modelId: "qwen3:0.6b"
			temperature: 0.1
		embeddingModel:
			modelId: "nomic-embed-text"
			temperature: 0.1
```

### Using OpenAI
1. Have your OpenAI API key available.
2. Set `chatbot.openai.apiKey` to your valid OpenAI API key.
3. Set `chatbot.service.enabled` to `true`.
4. Set `chatbot.chatModel.provider` and `embeddingModel.provider` to `"openai"`.
5. Tweak other configuration options as needed, such as:
	- `openai.chatModel.modelName`
	- `openai.chatModel.temperature`

**Example configuration:**
```yaml
chatbot:
  service:
    enabled: true
  chatModel:
    provider: "openai"
  embeddingModel:
    provider: "openai"
  openai:
    apiKey: "sk-change-me"  # replace with your real key
    chatModel:
      modelName: "gpt-3.5-turbo"
      temperature: 0.2
```

### EasyRAG Component
The chatbot supports RAG (Retrieval-Augmented Generation) using LangChain4j's EasyRAG component.
You can load RAG data via two approaches:

**Classpath (default)**

RAG data is bundled in the application’s classpath.
Place your files under `esthesis-core-backend/services/srv-chatbot/srv-chatbot-impl/src/main/resources/rag`.

**Example configuration:**
```yaml
chatbot:
	easyRag:
		path: "rag"
    pathType: "classpath"
```

**File System**

RAG data is stored in a mounted volume.
You must mount the target directory into the POD and configure the path accordingly.

**Example configuration:**
```yaml
chatbot:
	easyRag:
		path: "/app/rag"
		pathType: "filesystem"
```

### Notes
- You can combine this configuration in your custom `values.yaml` or use `--set` overrides.
- Ensure secrets (like OpenAI keys) are not hardcoded in production — use Kubernetes secrets instead.
- The chatbot service is only deployed when `chatbot.service.enabled` is `true`.
