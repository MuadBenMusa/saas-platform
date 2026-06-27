export interface Project {
  id: string;
  projectNumber: string;
  name: string;
  description?: string | null;
  status: 'ACTIVE' | 'ARCHIVED';
  tenantId: string;
  createdAt: string;
  updatedAt: string;
}
