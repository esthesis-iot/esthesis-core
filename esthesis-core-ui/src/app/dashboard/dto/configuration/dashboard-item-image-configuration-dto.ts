// Test public webcam:
// Video: http://212.26.235.210/axis-cgi/mjpg/video.cgi
// Snapshot: http://212.26.235.210/axis-cgi/jpg/image.cgi
export interface DashboardItemImageConfigurationDto {
  imageUrl: string;
  refresh: number;
  height: number;
}
