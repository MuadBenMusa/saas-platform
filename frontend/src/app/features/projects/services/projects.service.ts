import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Project } from '../models/project.model';
import { CreateProjectRequest } from '../models/create-project-request.model';
import { UpdateProjectRequest } from '../models/update-project-request.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectsService {
  constructor(private readonly http: HttpClient) {}

  findAll() {
    return this.http.get<Project[]>('/api/projects');
  }

  create(request: CreateProjectRequest) {
    return this.http.post<Project>('/api/projects', request);
  }

  findById(id: string) {
    return this.http.get<Project>(`/api/projects/${id}`);
  }

  update(id: string, request: UpdateProjectRequest) {
    return this.http.patch<Project>(`/api/projects/${id}`, request);
  }

  archive(id: string) {
    return this.http.delete<void>(`/api/projects/${id}`);
  }
}
