// Language Switching Functionality
document.addEventListener('DOMContentLoaded', () => {
    // Get saved language or default to 'en'
    let currentLang = localStorage.getItem('preferred-language') || 'en';
    
    // Initialize the page with the current language
    setLanguage(currentLang);
    
    // Add click handlers to language buttons
    const langButtons = document.querySelectorAll('.lang-btn');
    langButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const lang = btn.getAttribute('data-lang');
            setLanguage(lang);
            
            // Update active button state
            langButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            // Save preference
            localStorage.setItem('preferred-language', lang);
        });
    });
    
    // Set initial active button
    document.querySelector(`[data-lang="${currentLang}"]`)?.classList.add('active');
    langButtons.forEach(b => {
        if (b.getAttribute('data-lang') !== currentLang) {
            b.classList.remove('active');
        }
    });
    
    // Navbar scroll effect
    const navbar = document.querySelector('.language-nav');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.9)';
            navbar.style.boxShadow = 'none';
        }
    });
    
    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Intersection Observer for fade-in animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Apply initial hidden state and observe elements
    const animatedElements = document.querySelectorAll('.feature-card, .stat-card, .screenshot-card, .tech-item');
    animatedElements.forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = `opacity 0.6s ease ${index * 0.1}s, transform 0.6s ease ${index * 0.1}s`;
        observer.observe(el);
    });
});

function setLanguage(lang) {
    // Update HTML lang attribute
    document.documentElement.lang = lang;
    
    // Get translation object
    const t = translations[lang];
    if (!t) return;
    
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (t[key]) {
            el.textContent = t[key];
        }
    });
    
    // Update hero title for German
    if (lang === 'de') {
        document.querySelector('.hero-title').textContent = 'Software EKG';
        document.querySelector('.logo-text').textContent = 'Software EKG';
        document.querySelector('.footer-logo .logo-text').textContent = 'Software EKG';
    } else {
        document.querySelector('.hero-title').textContent = 'Software ECG';
        document.querySelector('.logo-text').textContent = 'Software ECG';
        document.querySelector('.footer-logo .logo-text').textContent = 'Software ECG';
    }
}

// Add some visual polish - particles effect (optional, lightweight)
function createParticle() {
    const hero = document.querySelector('.hero');
    if (!hero) return;
    
    const particle = document.createElement('div');
    particle.style.cssText = `
        position: absolute;
        width: 4px;
        height: 4px;
        background: rgba(99, 102, 241, 0.3);
        border-radius: 50%;
        pointer-events: none;
        left: ${Math.random() * 100}%;
        top: 100%;
        animation: floatUp ${5 + Math.random() * 5}s linear forwards;
    `;
    
    hero.appendChild(particle);
    
    setTimeout(() => particle.remove(), 10000);
}

// Add float up animation
const style = document.createElement('style');
style.textContent = `
    @keyframes floatUp {
        to {
            top: -10%;
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Create particles periodically
setInterval(createParticle, 500);

// Lightbox functionality
document.addEventListener('DOMContentLoaded', () => {
    const lightbox = document.getElementById('lightbox');
    const lightboxImg = document.getElementById('lightbox-img');
    const lightboxCaption = document.getElementById('lightbox-caption');
    const closeBtn = document.querySelector('.lightbox-close');
    
    // Add click handlers to all screenshot cards
    document.querySelectorAll('.screenshot-card').forEach(card => {
        card.addEventListener('click', () => {
            const img = card.querySelector('img');
            const caption = card.querySelector('.screenshot-overlay span');
            
            lightboxImg.src = img.src;
            lightboxCaption.textContent = caption ? caption.textContent : '';
            lightbox.classList.add('active');
            document.body.style.overflow = 'hidden';
        });
    });
    
    // Close lightbox on X click
    closeBtn.addEventListener('click', closeLightbox);
    
    // Close lightbox on background click
    lightbox.addEventListener('click', (e) => {
        if (e.target === lightbox) {
            closeLightbox();
        }
    });
    
    // Close lightbox on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && lightbox.classList.contains('active')) {
            closeLightbox();
        }
    });
    
    function closeLightbox() {
        lightbox.classList.remove('active');
        document.body.style.overflow = '';
    }
});
