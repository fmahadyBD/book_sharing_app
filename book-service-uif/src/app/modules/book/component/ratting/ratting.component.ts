import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-ratting',
  standalone: false,
  templateUrl: './ratting.component.html',
  styleUrl: './ratting.component.scss'
})
export class RattingComponent {
  
  @Input() rating: number = 0;
  @Output() ratingClicked: EventEmitter<number> = new EventEmitter<number>();
  maxRating: number = 5;

  get fullStars(): number {
    return Math.floor(this.rating);
  }

  get hasHalfStar(): boolean {
    return this.rating % 1 !== 0;
  }

  get emptyStars(): number {
    return this.maxRating - Math.ceil(this.rating);
  }
}
