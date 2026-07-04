import { Component, OnInit, signal } from '@angular/core';
import { SessionService } from '../../core/auth/session.service';
import { ProjectsService } from '../projects/services/projects.service';
import { PageHeaderComponent } from '../../shared/ui/page-header/page-header.component';
import { AppCardComponent } from '../../shared/ui/app-card/app-card.component';
import { StatusBadgeComponent } from '../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [PageHeaderComponent, AppCardComponent, StatusBadgeComponent],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit {
  readonly projectCount = signal<number | null>(null);
  readonly projectCountLoading = signal(false);
  readonly projectCountError = signal(false);

  constructor(
    public readonly sessionService: SessionService,
    private readonly projectsService: ProjectsService
  ) {}

  ngOnInit(): void {
    this.loadProjectCount();
  }

  private loadProjectCount(): void {
    this.projectCountLoading.set(true);
    this.projectCountError.set(false);

    this.projectsService.findAll().subscribe({
      next: projects => {
        this.projectCount.set(projects.length);
        this.projectCountLoading.set(false);
      },
      error: () => {
        this.projectCount.set(null);
        this.projectCountError.set(true);
        this.projectCountLoading.set(false);
      }
    });
  }
}
