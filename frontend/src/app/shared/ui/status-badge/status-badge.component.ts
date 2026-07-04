import { Component, Input } from '@angular/core';

export type StatusBadgeStatus =
  | 'ACTIVE'
  | 'ARCHIVED'
  | 'LOW_STOCK'
  | 'ORDERED'
  | 'DELIVERED'
  | 'DRAFT'
  | 'UNKNOWN'
  | string;

@Component({
  selector: 'app-status-badge',
  standalone: true,
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss'
})
export class StatusBadgeComponent {
  @Input() status: StatusBadgeStatus = 'UNKNOWN';
  @Input() label = '';

  readonly knownStatuses = new Set([
    'ACTIVE',
    'ARCHIVED',
    'LOW_STOCK',
    'ORDERED',
    'DELIVERED',
    'DRAFT'
  ]);

  get normalizedStatus(): string {
    const normalized = String(this.status || 'UNKNOWN').toUpperCase();
    return this.knownStatuses.has(normalized) ? normalized : 'UNKNOWN';
  }

  get displayLabel(): string {
    return this.label || String(this.status || 'Unknown').replaceAll('_', ' ');
  }
}
