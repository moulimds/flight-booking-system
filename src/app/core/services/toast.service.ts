import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  title: string;
  message: string;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  public toasts$ = this.toastsSubject.asObservable();

  show(type: 'success' | 'error' | 'info' | 'warning', title: string, message: string, durationMs = 4000) {
    const id = Math.random().toString(36).substring(2, 9);
    const toast: ToastMessage = { id, type, title, message, timestamp: new Date() };
    const current = this.toastsSubject.getValue();
    this.toastsSubject.next([...current, toast]);

    if (durationMs > 0) {
      setTimeout(() => {
        this.remove(id);
      }, durationMs);
    }
  }

  success(title: string, message: string) {
    this.show('success', title, message);
  }

  error(title: string, message: string) {
    this.show('error', title, message, 6000);
  }

  info(title: string, message: string) {
    this.show('info', title, message);
  }

  warning(title: string, message: string) {
    this.show('warning', title, message);
  }

  remove(id: string) {
    const current = this.toastsSubject.getValue().filter(t => t.id !== id);
    this.toastsSubject.next(current);
  }
}
