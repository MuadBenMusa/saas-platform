import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input() mobileOpen = false;

  @Output() navigate = new EventEmitter<void>();

  readonly collapsed = signal(false);

  toggleCollapsed(): void {
    this.collapsed.update(collapsed => !collapsed);
  }

  handleNavigate(): void {
    this.navigate.emit();
  }
}
