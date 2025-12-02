#!/usr/bin/env python3
"""
Create basic Android launcher icons for the Zero-Trust Banking Demo app.
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Define icon sizes for different densities
sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

# Base directory
res_dir = 'app/src/main/res'

# Colors for the Zero-Trust Banking app
bg_color = '#1e3a8a'  # Deep blue
accent_color = '#06b6d4'  # Cyan

def hex_to_rgb(hex_color):
    """Convert hex color to RGB tuple."""
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

def create_square_icon(size):
    """Create a square launcher icon."""
    img = Image.new('RGB', (size, size), hex_to_rgb(bg_color))
    draw = ImageDraw.Draw(img)
    
    # Draw a shield shape (simplified as a pentagon)
    padding = size // 6
    width = size - 2 * padding
    height = size - 2 * padding
    
    # Shield points
    points = [
        (size // 2, padding),  # Top center
        (padding, padding + height // 3),  # Top left
        (padding, padding + 2 * height // 3),  # Bottom left
        (size // 2, size - padding),  # Bottom center
        (size - padding, padding + 2 * height // 3),  # Bottom right
        (size - padding, padding + height // 3),  # Top right
    ]
    
    draw.polygon(points, fill=hex_to_rgb(accent_color))
    
    # Draw a lock symbol in the center
    lock_size = size // 4
    lock_x = size // 2 - lock_size // 2
    lock_y = size // 2 - lock_size // 4
    
    # Lock body (rectangle)
    draw.rectangle(
        [lock_x, lock_y + lock_size // 3, lock_x + lock_size, lock_y + lock_size],
        fill=hex_to_rgb(bg_color)
    )
    
    # Lock shackle (arc)
    shackle_padding = lock_size // 4
    draw.arc(
        [lock_x + shackle_padding, lock_y, 
         lock_x + lock_size - shackle_padding, lock_y + lock_size // 2],
        start=0, end=180, fill=hex_to_rgb(bg_color), width=size//24
    )
    
    return img

def create_round_icon(size):
    """Create a round launcher icon."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Draw circle background
    draw.ellipse([0, 0, size-1, size-1], fill=hex_to_rgb(bg_color))
    
    # Draw a shield shape
    padding = size // 5
    width = size - 2 * padding
    height = size - 2 * padding
    
    points = [
        (size // 2, padding),
        (padding, padding + height // 3),
        (padding, padding + 2 * height // 3),
        (size // 2, size - padding),
        (size - padding, padding + 2 * height // 3),
        (size - padding, padding + height // 3),
    ]
    
    draw.polygon(points, fill=hex_to_rgb(accent_color))
    
    # Draw a lock
    lock_size = size // 4
    lock_x = size // 2 - lock_size // 2
    lock_y = size // 2 - lock_size // 4
    
    draw.rectangle(
        [lock_x, lock_y + lock_size // 3, lock_x + lock_size, lock_y + lock_size],
        fill=hex_to_rgb(bg_color)
    )
    
    draw.arc(
        [lock_x + lock_size // 4, lock_y, 
         lock_x + 3 * lock_size // 4, lock_y + lock_size // 2],
        start=0, end=180, fill=hex_to_rgb(bg_color), width=max(2, size//24)
    )
    
    return img

# Create icons for all densities
for density, size in sizes.items():
    mipmap_dir = f'{res_dir}/mipmap-{density}'
    os.makedirs(mipmap_dir, exist_ok=True)
    
    # Create square icon
    square_icon = create_square_icon(size)
    square_icon.save(f'{mipmap_dir}/ic_launcher.png')
    print(f'Created {mipmap_dir}/ic_launcher.png ({size}x{size})')
    
    # Create round icon
    round_icon = create_round_icon(size)
    round_icon.save(f'{mipmap_dir}/ic_launcher_round.png')
    print(f'Created {mipmap_dir}/ic_launcher_round.png ({size}x{size})')

print('\nAll launcher icons created successfully!')
