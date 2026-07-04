import { Component, OnInit, computed, signal } from '@angular/core';
import { Project } from '../models/project.model';
import { ProjectsService } from '../services/projects.service';
import { CreateProjectRequest } from '../models/create-project-request.model';
import { ProjectFormComponent } from '../components/project-form/project-form.component';
import { ProjectListComponent } from '../components/project-list/project-list.component';
import { PageHeaderComponent } from '../../../shared/ui/page-header/page-header.component';
import { EmptyStateComponent } from '../../../shared/ui/empty-state/empty-state.component';
import { AppCardComponent } from '../../../shared/ui/app-card/app-card.component';
import {
  SearchToolbarComponent,
  SearchToolbarStatus
} from '../../../shared/ui/search-toolbar/search-toolbar.component';


@Component({
  selector: 'app-projects-page',
  standalone: true,
  imports: [
    ProjectFormComponent,
    ProjectListComponent,
    PageHeaderComponent,
    EmptyStateComponent,
    AppCardComponent,
    SearchToolbarComponent
  ],
  templateUrl: './projects-page.component.html',
  styleUrl: './projects-page.component.scss'
})
export class ProjectsPageComponent implements OnInit {
  readonly projects = signal<Project[]>([]);
  readonly searchTerm = signal('');
  readonly statusFilter = signal<SearchToolbarStatus>('ALL');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly creating = signal(false);
  readonly archiving = signal(false);
  readonly isCreateDialogOpen = signal(false);
  readonly projectPendingArchive = signal<Project | null>(null);
  readonly filteredProjects = computed(() => {
    const search = this.searchTerm().trim().toLowerCase();
    const statusFilter = this.statusFilter();

    return this.projects().filter(project => {
      const matchesStatus = statusFilter === 'ALL' || project.status === statusFilter;
      const matchesSearch =
        !search ||
        project.projectNumber.toLowerCase().includes(search) ||
        project.name.toLowerCase().includes(search) ||
        project.status.toLowerCase().includes(search);

      return matchesStatus && matchesSearch;
    });
  });
  readonly hasActiveFilters = computed(() =>
    this.searchTerm().trim().length > 0 || this.statusFilter() !== 'ALL'
  );
  readonly resultLabel = computed(() => {
    const total = this.projects().length;
    const visible = this.filteredProjects().length;
    return `${visible} of ${total} project${total === 1 ? '' : 's'}`;
  });

  constructor(private readonly projectsService: ProjectsService) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);
    this.error.set(null);

    this.projectsService.findAll().subscribe({
      next: projects => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load projects right now. Please try again.');
        this.loading.set(false);
      }
    });
  }

  openCreateDialog(): void {
    this.error.set(null);
    this.isCreateDialogOpen.set(true);
  }

  closeCreateDialog(): void {
    if (this.creating()) {
      return;
    }

    this.error.set(null);
    this.isCreateDialogOpen.set(false);
  }

  updateSearchTerm(value: string): void {
    this.searchTerm.set(value);
  }

  updateStatusFilter(value: SearchToolbarStatus): void {
    this.statusFilter.set(value);
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('ALL');
  }

  createProject(request: CreateProjectRequest): void {
    this.creating.set(true);
    this.error.set(null);

    this.projectsService.create(request).subscribe({
      next: createdProject => {
        this.projects.update(projects => [createdProject, ...projects]);
        this.creating.set(false);
        this.isCreateDialogOpen.set(false);
      },
      error: error => {
        this.creating.set(false);

        if (error.status === 409) {
          this.error.set('A project with this number already exists in the current tenant.');
          return;
        }

        this.error.set('We could not create the project. Please check the details and try again.');
      }
    });
  }

  openArchiveDialog(project: Project): void {
    this.error.set(null);
    this.projectPendingArchive.set(project);
  }

  closeArchiveDialog(): void {
    if (this.archiving()) {
      return;
    }

    this.error.set(null);
    this.projectPendingArchive.set(null);
  }

  confirmArchiveProject(): void {
    const project = this.projectPendingArchive();

    if (!project) {
      return;
    }

    this.archiving.set(true);
    this.error.set(null);

    this.projectsService.archive(project.id).subscribe({
      next: () => {
        this.projects.update(projects =>
          projects.map(existing =>
            existing.id === project.id
              ? { ...existing, status: 'ARCHIVED' }
              : existing
          )
        );
        this.archiving.set(false);
        this.projectPendingArchive.set(null);
      },
      error: () => {
        this.archiving.set(false);
        this.error.set('We could not archive this project right now. Please try again.');
      }
    });
  }
}
