import { Component } from '@angular/core';
import { SessionService } from '../../core/auth/session.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent {
  constructor(public readonly sessionService: SessionService) {}
}
