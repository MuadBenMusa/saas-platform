import { Component, EventEmitter, HostListener, Input, Output, signal } from '@angular/core';
import { Project } from '../../models/project.model';
import { DatePipe } from '@angular/common';
import { StatusBadgeComponent } from '../../../../shared/ui/status-badge/status-badge.component';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [DatePipe, StatusBadgeComponent],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.scss'
})
export class ProjectListComponent {
  @Input({ required: true }) projects: Project[] = [];

  @Output() archiveProject = new EventEmitter<Project>();

  readonly openActionsProjectId = signal<string | null>(null);

  toggleActionsMenu(project: Project, event: MouseEvent): void {
    event.stopPropagation();
    this.openActionsProjectId.update(currentProjectId =>
      currentProjectId === project.id ? null : project.id
    );
  }

  archiveFromMenu(project: Project, event: MouseEvent): void {
    event.stopPropagation();
    this.openActionsProjectId.set(null);
    this.archiveProject.emit(project);
  }

  @HostListener('document:click')
  closeActionsMenu(): void {
    this.openActionsProjectId.set(null);
  }

  @HostListener('document:keydown.escape')
  closeActionsMenuOnEscape(): void {
    this.openActionsProjectId.set(null);
  }
}
