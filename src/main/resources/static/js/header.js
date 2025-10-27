
        // Highlight active navigation based on current page
        document.addEventListener('DOMContentLoaded', function() {
            const currentPath = window.location.pathname;
            const navLinks = document.querySelectorAll('.nav-link');
            
            navLinks.forEach(link => {
                const href = link.getAttribute('href');
                if (currentPath === href || currentPath.startsWith(href + '/')) {
                    link.classList.add('active');
                } else {
                    link.classList.remove('active');
                }
            });
        });

        // Close mobile menu when clicking outside
        document.addEventListener('click', function(event) {
            const navbar = document.querySelector('.navbar-collapse');
            const toggler = document.querySelector('.navbar-toggler');
            
            if (navbar.classList.contains('show') && 
                !navbar.contains(event.target) && 
                !toggler.contains(event.target)) {
                toggler.click();
            }
        });
