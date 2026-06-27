export interface CreateProjectRequest {
  projectNumber: string;
  name: string;
  description?: string | null;
}
