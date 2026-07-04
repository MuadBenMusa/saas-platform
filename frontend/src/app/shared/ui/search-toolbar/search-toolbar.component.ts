import { Component, EventEmitter, Input, Output } from '@angular/core';

export type SearchToolbarStatus = 'ALL' | 'ACTIVE' | 'ARCHIVED';

@Component({
  selector: 'app-search-toolbar',
  standalone: true,
  templateUrl: './search-toolbar.component.html',
  styleUrl: './search-toolbar.component.scss'
})
export class SearchToolbarComponent {
  @Input() searchValue = '';
  @Input() statusFilter: SearchToolbarStatus = 'ALL';
  @Input() placeholder = 'Search';
  @Input() resultLabel = '';
  @Input() canClear = true;

  @Output() searchValueChange = new EventEmitter<string>();
  @Output() statusFilterChange = new EventEmitter<SearchToolbarStatus>();
  @Output() clearFilters = new EventEmitter<void>();

  readonly filters: SearchToolbarStatus[] = ['ALL', 'ACTIVE', 'ARCHIVED'];

  filterLabel(filter: SearchToolbarStatus): string {
    return filter === 'ALL' ? 'All' : filter[0] + filter.slice(1).toLowerCase();
  }
}
