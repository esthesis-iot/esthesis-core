export interface ChatbotMessageDto {
  message: string;
  correlationId: string;
  timestamp: number;
  isUserInput: boolean;
  isError?: boolean;
}
