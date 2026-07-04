import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SessionService } from '../../core/auth/session.service';
import { CurrentSession } from '../../core/auth/session.model';

@Component({
  selector: 'app-topbar',
  standalone: true,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent {
  @Input() session: CurrentSession | null = null;
  @Input() mobileMenuOpen = false;

  @Output() toggleMobileMenu = new EventEmitter<void>();

  constructor(public readonly sessionService: SessionService) {}
}
