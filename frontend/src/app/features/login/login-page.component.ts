import { Component, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SessionService } from '../../core/auth/session.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {
  readonly loggedOut = signal(false);

  constructor(
    route: ActivatedRoute,
    public readonly sessionService: SessionService
  ) {
    this.loggedOut.set(route.snapshot.queryParamMap.has('logout'));
  }
}
