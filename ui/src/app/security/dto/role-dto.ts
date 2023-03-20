export interface RoleDto {
  id: string;
  name: string;
  description: string | null;
  policies: string[];
}
