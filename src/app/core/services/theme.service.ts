import { Injectable, signal, effect } from '@angular/core';

export type AppTheme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'skywings_theme';
  public currentTheme = signal<AppTheme>('light');

  constructor() {
    const savedTheme = (localStorage.getItem(this.THEME_KEY) as AppTheme) || 'light';
    this.currentTheme.set(savedTheme);
    this.applyTheme(savedTheme);

    // Reactively update HTML/body when currentTheme signal changes
    effect(() => {
      const theme = this.currentTheme();
      this.applyTheme(theme);
      localStorage.setItem(this.THEME_KEY, theme);
    });
  }

  public toggleTheme(): void {
    const next = this.currentTheme() === 'dark' ? 'light' : 'dark';
    this.currentTheme.set(next);
  }

  public setTheme(theme: AppTheme): void {
    this.currentTheme.set(theme);
  }

  public isDark(): boolean {
    return this.currentTheme() === 'dark';
  }

  private applyTheme(theme: AppTheme): void {
    if (typeof document !== 'undefined') {
      const root = document.documentElement;
      const body = document.body;
      if (theme === 'dark') {
        root.classList.add('dark-theme', 'dark');
        body.classList.add('dark-theme', 'dark');
      } else {
        root.classList.remove('dark-theme', 'dark');
        body.classList.remove('dark-theme', 'dark');
      }
    }
  }
}
