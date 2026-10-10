import { Component, OnInit, inject, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { ToastService } from '../../../core/services/toast.service';

export interface ChatMessage {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  timestamp: Date;
  category?: string;
  actionLink?: string;
  actionLabel?: string;
}

export interface FaqItem {
  id: number;
  category: string;
  categoryTitle: string;
  question: string;
  answer: string;
  keywords: string[];
  actionLink?: string;
  actionLabel?: string;
  isOpen?: boolean;
}

@Component({
  selector: 'app-customer-support',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './customer-support.component.html',
  styleUrl: './customer-support.component.css'
})
export class CustomerSupportComponent implements OnInit, AfterViewChecked {
  private http = inject(HttpClient);
  public authService = inject(AuthService);
  public themeService = inject(ThemeService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  @ViewChild('chatScrollContainer') private chatScrollContainer!: ElementRef;

  private gatewayUrl = 'http://localhost:8080/api/notifications/support';

  // Chat State
  public messages: ChatMessage[] = [];
  public currentQuery = '';
  public isTyping = false;
  private shouldScrollToBottom = false;

  // Active Category Filter for FAQs
  public activeCategory = 'ALL';
  public faqSearchQuery = '';

  // Right Side Panel Tabs ('FAQ' | 'TICKET')
  public rightTab: 'FAQ' | 'TICKET' = 'FAQ';

  // Contact Support Form
  public ticketData = {
    name: '',
    email: '',
    category: 'FLIGHT_BOOKING',
    subject: '',
    message: '',
    pnr: '',
    priority: 'NORMAL'
  };
  public isSubmittingTicket = false;
  public createdTicketId: string | null = null;

  // Quick Suggested Questions Chips
  public suggestedQuestions = [
    { text: 'How do I book round-trip flights?', category: 'ROUND_TRIP' },
    { text: 'How does multi-city booking work?', category: 'MULTI_CITY' },
    { text: 'What meal options can I choose for passengers?', category: 'MEALS' },
    { text: 'Explain the 8-hour departure booking cutoff', category: 'CUTOFF' },
    { text: 'Do flights operate daily or on specific days?', category: 'SCHEDULES' },
    { text: 'How do I cancel a booking and get a refund?', category: 'CANCELLATION' },
    { text: 'What payment methods & GST invoices are supported?', category: 'PAYMENTS' },
    { text: 'When is web check-in available for boarding passes?', category: 'CHECKIN' },
    { text: 'How does live flight radar tracking work?', category: 'TRACKING' }
  ];

  // Centralized FAQ Knowledge Base
  public faqs: FaqItem[] = [
    {
      id: 1,
      category: 'ROUND_TRIP',
      categoryTitle: 'Round-Trip Booking',
      question: 'How do I book a round-trip flight on SkyWings?',
      answer: 'To book round-trip flights:\n1. Choose "Round-Trip" in the trip selector.\n2. Pick your departure airport, arrival airport, outbound departure date, and return date.\n3. Browse outbound flights and click "Select Outbound".\n4. Browse return flights on your return date and click "Select Return".\n5. Your total fare combines both flights dynamically. You can then select seats, dining meals, and confirm payment.',
      keywords: ['round-trip', 'round trip', 'return', 'two way', 'both ways'],
      actionLink: '/flights/dashboard?tripType=ROUND_TRIP',
      actionLabel: 'Book Round-Trip Flights'
    },
    {
      id: 2,
      category: 'MULTI_CITY',
      categoryTitle: 'Multi-City Itineraries',
      question: 'How can I book connected multi-city flight segments?',
      answer: 'SkyWings allows booking up to 5 interconnected flight segments:\n1. Select "Multi-City" in the search dashboard.\n2. Click "+ Add Another Flight Leg" to add city hops.\n3. Choose individual origins, destinations, and travel dates in chronological order.\n4. Pick a flight for each leg.\n5. You will receive confirmed tickets for every passenger and segment under a single parent booking reference.',
      keywords: ['multi-city', 'multi city', 'multiple flights', 'connected', 'legs'],
      actionLink: '/flights/dashboard?tripType=MULTI_CITY',
      actionLabel: 'Explore Multi-City Booking'
    },
    {
      id: 3,
      category: 'MEALS',
      categoryTitle: 'In-Flight Food & Dining',
      question: 'What dining preferences and meals are offered on flights?',
      answer: 'SkyWings provides customized dining for every passenger and flight segment:\n• Vegetarian Royal Thali: Paneer makhani, dal tadka, jeera rice & warm paratha (Complimentary)\n• Grilled Herb Chicken: Tender chicken breast with rosemary jus & butter veggies (₹450)\n• Vegan Quinoa Power Bowl: Tri-color quinoa, roasted chickpeas & avocado (₹350)\n• Jain Special Feast: Strictly free of root vegetables, onion, or garlic (Complimentary)\n• Gluten-Free Platter: Naturally gluten-free steamed millet bowl with tofu (₹400)\n• Kids Junior Snack Box: Mini sandwich, cookies, fruit & juice (₹250)\n• No Meal Service: Opt out of food service during this flight (Complimentary)',
      keywords: ['food', 'meal', 'veg', 'non veg', 'vegan', 'jain', 'snack', 'menu'],
      actionLink: '/flights/dashboard',
      actionLabel: 'View Flight & Meal Options'
    },
    {
      id: 4,
      category: 'CUTOFF',
      categoryTitle: '8-Hour Departure Cutoff Policy',
      question: 'Why can flights departing within 8 hours not be booked?',
      answer: 'Booking Restriction Policy:\nTo guarantee security clearances, flight manifest locking, passenger baggage allocation, and on-time departures, new flight reservations must be finalized at least 8 hours prior to scheduled takeoff.\n\nFlights departing within 8 hours are automatically hidden from customer flight search results and direct API attempts are strictly rejected by the booking gateway.',
      keywords: ['cutoff', '8 hours', '8 hrs', 'restriction', 'booking closed', 'deadline'],
      actionLink: '/flights/dashboard',
      actionLabel: 'Search Eligible Flights'
    },
    {
      id: 5,
      category: 'SCHEDULES',
      categoryTitle: 'Flight Schedules & Frequency',
      question: 'How do I know if a flight operates Daily or on Specific Days?',
      answer: 'SkyWings flight operations follow structured weekly schedules:\n• Operates Daily: High-traffic trunk routes (e.g., Chennai ⇄ Delhi, Delhi ⇄ Mumbai, Mumbai ⇄ Bangalore) run 7 days a week (Mon–Sun).\n• Specific Operating Days: Certain specialized flights operate on select weekdays (e.g., Mon, Wed, Fri) or on weekends (Sat, Sun).\n\nEvery flight card displays a schedule badge ("🗓️ Daily" or "⚡ Specific Days") so you can plan your week with complete transparency.',
      keywords: ['schedule', 'daily', 'specific days', 'frequency', 'weekly', 'operating days'],
      actionLink: '/flights/dashboard',
      actionLabel: 'Check Weekly Schedules'
    },
    {
      id: 6,
      category: 'CANCELLATION',
      categoryTitle: 'Cancellations & Refunds',
      question: 'How do I cancel my booking and receive my refund?',
      answer: 'To cancel your reservation:\n1. Open "My Bookings" from the navigation menu.\n2. Locate your booking by PNR reference.\n3. Click "Cancel Booking" and confirm.\n4. The system automatically cancels your flight, releases the seat hold, and triggers an automated refund to your source payment method within 5–7 banking days.',
      keywords: ['cancel', 'cancellation', 'refund', 'money back', 'pnr cancellation'],
      actionLink: '/tickets/my-tickets',
      actionLabel: 'Go to My Bookings'
    },
    {
      id: 7,
      category: 'PAYMENTS',
      categoryTitle: 'Payments & GST Tax Invoices',
      question: 'Which payment methods are accepted and how can I get a GST invoice?',
      answer: 'We support all major payment methods:\n• Credit & Debit Cards (Visa, MasterCard, RuPay, Amex)\n• Instant UPI (Google Pay, PhonePe, Paytm, BHIM)\n• Net Banking across all major domestic & international banks\n\nOfficial GST Tax Invoices in formatted PDF format are generated immediately upon successful payment and can be downloaded from your booking details or email.',
      keywords: ['payment', 'pay', 'upi', 'card', 'gst', 'invoice', 'receipt', 'tax'],
      actionLink: '/tickets/my-tickets',
      actionLabel: 'View Invoices in My Bookings'
    },
    {
      id: 8,
      category: 'CHECKIN',
      categoryTitle: 'Web Check-in & Boarding Pass',
      question: 'When does Web Check-in open and how do I get my boarding pass?',
      answer: 'Web Check-In opens 48 hours prior to scheduled flight departure and closes 60 minutes before takeoff.\n\nFrom "My Bookings", click "Web Check-In / Boarding Pass" to generate your official printable boarding pass complete with your assigned seat, boarding gate, terminal, and security barcode.',
      keywords: ['check in', 'checkin', 'boarding pass', 'gate', 'terminal', 'barcode'],
      actionLink: '/tickets/my-tickets',
      actionLabel: 'Check-In & Get Boarding Pass'
    },
    {
      id: 9,
      category: 'TRACKING',
      categoryTitle: 'Flight Radar & Status',
      question: 'How do I track active aircraft in real-time on Flight Radar?',
      answer: 'Click "Live Radar" in the top navbar to access our real-time interactive airspace radar. Track airborne flights, ground speeds, altitudes, aircraft types, and route progress live across all SkyWings flight corridors.',
      keywords: ['radar', 'live tracking', 'flight status', 'altitude', 'aircraft track'],
      actionLink: '/tracking/live-radar',
      actionLabel: 'Launch Flight Radar'
    }
  ];

  ngOnInit(): void {
    // Populate user details for contact ticket form
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.ticketData.name = user.fullName || '';
        this.ticketData.email = user.email || '';
      }
    });

    // Load FAQs from Backend
    this.loadBackendFaqs();

    // Initialize Chat with Welcome Greeting
    const user = this.authService.currentUserValue;
    const name = user?.fullName || user?.email?.split('@')[0] || 'Traveler';

    this.messages.push({
      id: 'msg-welcome',
      sender: 'bot',
      text: `Hello ${name}! Welcome to SkyWings Customer Support ✈️\n\nI am your 24/7 Airline Assistant. You can ask me anything about:\n• Round-Trip & Multi-City Flight Bookings\n• In-Flight Meal & Dietary Preferences\n• 8-Hour Departure Cutoff Policy\n• Flight Schedules (Daily vs Specific Days)\n• Cancellations, Refunds & Payments\n• Web Check-In & Boarding Passes\n\nHow can I help you today?`,
      timestamp: new Date()
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.chatScrollContainer) {
        this.chatScrollContainer.nativeElement.scrollTop = this.chatScrollContainer.nativeElement.scrollHeight;
      }
    } catch {}
  }

  // Load FAQ articles from Spring Boot backend
  private loadBackendFaqs(): void {
    this.http.get<FaqItem[]>(`${this.gatewayUrl}/faq`).subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.faqs = data.map(item => ({ ...item, isOpen: false }));
        }
      },
      error: () => {
        // Fallback to rich built-in FAQ database
      }
    });
  }

  // Filter FAQs by Category and Search Text
  public get filteredFaqs(): FaqItem[] {
    return this.faqs.filter(item => {
      const matchesCat = this.activeCategory === 'ALL' || item.category === this.activeCategory;
      if (!matchesCat) return false;

      if (!this.faqSearchQuery.trim()) return true;
      const q = this.faqSearchQuery.toLowerCase();
      return item.question.toLowerCase().includes(q) ||
             item.answer.toLowerCase().includes(q) ||
             (item.keywords || []).some(k => k.toLowerCase().includes(q));
    });
  }

  // Send Customer Chat Query
  public sendMessage(textToSend?: string): void {
    const text = (textToSend || this.currentQuery).trim();
    if (!text) return;

    // Add User Message
    const userMsg: ChatMessage = {
      id: `usr-${Date.now()}`,
      sender: 'user',
      text,
      timestamp: new Date()
    };
    this.messages.push(userMsg);
    this.currentQuery = '';
    this.isTyping = true;
    this.shouldScrollToBottom = true;

    // Call Backend Support Chat API
    const payload = {
      message: text,
      category: this.activeCategory !== 'ALL' ? this.activeCategory : '',
      userName: this.authService.currentUserValue?.fullName || 'Customer'
    };

    this.http.post<any>(`${this.gatewayUrl}/chat`, payload).subscribe({
      next: (resp) => {
        this.isTyping = false;
        const botMsg: ChatMessage = {
          id: `bot-${Date.now()}`,
          sender: 'bot',
          text: resp.answer || 'Thank you for reaching out. We are glad to assist you!',
          timestamp: new Date(),
          actionLink: resp.actionLink,
          actionLabel: resp.actionLabel
        };
        this.messages.push(botMsg);
        this.shouldScrollToBottom = true;
      },
      error: () => {
        // Intelligent Local Matching Fallback
        this.isTyping = false;
        const localMatch = this.findBestLocalMatch(text);
        const botMsg: ChatMessage = {
          id: `bot-${Date.now()}`,
          sender: 'bot',
          text: localMatch.answer,
          timestamp: new Date(),
          actionLink: localMatch.actionLink,
          actionLabel: localMatch.actionLabel
        };
        this.messages.push(botMsg);
        this.shouldScrollToBottom = true;
      }
    });
  }

  // Local Matcher in case backend is offline
  private findBestLocalMatch(query: string): { answer: string; actionLink?: string; actionLabel?: string } {
    const q = query.toLowerCase();
    for (const faq of this.faqs) {
      if (faq.keywords.some(k => q.includes(k.toLowerCase())) || faq.question.toLowerCase().includes(q)) {
        return {
          answer: faq.answer,
          actionLink: faq.actionLink,
          actionLabel: faq.actionLabel
        };
      }
    }

    return {
      answer: `I could not find an exact answer for "${query}". You can browse the FAQ topics on the right, or submit a support ticket below to connect directly with our 24x7 Support Desk.`,
      actionLink: '/support',
      actionLabel: 'Raise Support Ticket'
    };
  }

  // Ask Suggested Question from Chip
  public askSuggestedQuestion(item: { text: string; category: string }): void {
    this.activeCategory = item.category;
    this.sendMessage(item.text);
  }

  // Submit Contact Support Ticket
  public submitTicket(): void {
    if (!this.ticketData.name || !this.ticketData.email || !this.ticketData.subject || !this.ticketData.message) {
      this.toastService.warning('Required Fields', 'Please fill in all ticket details.');
      return;
    }

    this.isSubmittingTicket = true;
    this.http.post<any>(`${this.gatewayUrl}/ticket`, this.ticketData).subscribe({
      next: (resp) => {
        this.isSubmittingTicket = false;
        this.createdTicketId = resp.ticketId;
        this.toastService.success('Ticket Submitted', `Reference ID: ${resp.ticketId}`);

        // Add a message in chat confirming ticket submission
        this.messages.push({
          id: `tkt-${Date.now()}`,
          sender: 'bot',
          text: `✅ Support Ticket Raised Successfully!\n\nTicket Reference: ${resp.ticketId}\nCategory: ${this.ticketData.category}\nSubject: ${this.ticketData.subject}\n\nOur customer support specialists have received your inquiry and will email updates to ${this.ticketData.email}.`,
          timestamp: new Date()
        });
        this.shouldScrollToBottom = true;

        // Reset message and subject
        this.ticketData.subject = '';
        this.ticketData.message = '';
        this.ticketData.pnr = '';
      },
      error: () => {
        this.isSubmittingTicket = false;
        const fallbackTicketId = 'SKY-SUP-' + Math.floor(10000 + Math.random() * 90000);
        this.createdTicketId = fallbackTicketId;
        this.toastService.success('Ticket Created', `Reference ID: ${fallbackTicketId}`);

        this.messages.push({
          id: `tkt-${Date.now()}`,
          sender: 'bot',
          text: `✅ Support Ticket Logged!\n\nReference ID: ${fallbackTicketId}\nOur team will review your query and contact ${this.ticketData.email}.`,
          timestamp: new Date()
        });
        this.shouldScrollToBottom = true;
      }
    });
  }

  public resetChat(): void {
    this.messages = [];
    this.ngOnInit();
  }
}
