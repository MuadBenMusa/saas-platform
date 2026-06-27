export interface CurrentSession {
  user: CurrentUser;
  activeTenant: ActiveTenant;
}

export interface CurrentUser {
  id: string;
  email: string;
  name: string;
  status: string;
}

export interface ActiveTenant {
  id: string;
  name: string;
  slug: string;
  role: string;
}
