import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.scss'
})
export class EmptyStateComponent {
  @Input({ required: true }) title = '';
  @Input() message = '';
  @Input() icon = '+';
  @Input() primaryActionLabel = '';

  @Output() primaryAction = new EventEmitter<void>();
}
