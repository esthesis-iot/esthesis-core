export interface DashboardItemDto {
  id: string;
  type: string;
  title: string;
  subtitle?: string;
  columns: number;
  index: number;
  configuration?: string;
  enabled: boolean;
}
