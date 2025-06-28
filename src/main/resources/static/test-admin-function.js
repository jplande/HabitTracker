// Script à ajouter temporairement dans les templates admin pour tester
// ✅ Fonctions de test pour vérifier que tout fonctionne

console.log('🔧 Script de test Admin chargé');

// Test 1: Vérifier que les formulaires se soumettent correctement
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ DOM chargé, initialisation des tests...');

    // Test des formulaires
    const forms = document.querySelectorAll('form');
    console.log(`📋 ${forms.length} formulaire(s) détecté(s)`);

    forms.forEach((form, index) => {
        form.addEventListener('submit', function(e) {
            console.log(`📤 Soumission formulaire ${index + 1}:`, form.action);

            // Vérifier la présence du token CSRF
            const csrfToken = form.querySelector('input[name="_csrf"]');
            if (csrfToken) {
                console.log('✅ Token CSRF présent');
            } else {
                console.warn('⚠️ Token CSRF manquant !');
            }
        });
    });

    // Test des boutons d'action
    const actionButtons = document.querySelectorAll('[data-action]');
    console.log(`🔘 ${actionButtons.length} bouton(s) d'action détecté(s)`);

    // Test des alertes
    const alerts = document.querySelectorAll('.alert');
    if (alerts.length > 0) {
        console.log(`🚨 ${alerts.length} alerte(s) affichée(s)`);
        alerts.forEach(alert => {
            console.log(`   - ${alert.classList.contains('alert-success') ? '✅ Succès' :
                alert.classList.contains('alert-danger') ? '❌ Erreur' :
                    '💡 Info'}: ${alert.textContent.trim()}`);
        });
    }
});

// Test 2: Fonction pour tester manuellement les actions AJAX
function testAdminAction(action, userId) {
    console.log(`🧪 Test action: ${action} pour utilisateur ${userId}`);

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content ||
        document.querySelector('input[name="_csrf"]')?.value;

    if (!csrfToken) {
        console.error('❌ Token CSRF introuvable');
        return;
    }

    const formData = new FormData();
    formData.append('_csrf', csrfToken);

    fetch(`/admin/users/${userId}/${action}`, {
        method: 'POST',
        body: formData
    })
        .then(response => {
            console.log(`📡 Réponse ${action}:`, response.status);
            if (response.redirected) {
                console.log(`🔄 Redirection vers: ${response.url}`);
            }
            return response.text();
        })
        .then(html => {
            console.log(`✅ Action ${action} terminée`);
        })
        .catch(error => {
            console.error(`❌ Erreur ${action}:`, error);
        });
}

// Test 3: Fonction pour valider l'état de la page
function validateAdminPage() {
    console.log('🔍 Validation de la page admin...');

    const checks = {
        'CSRF Token': document.querySelector('input[name="_csrf"]') !== null,
        'Bootstrap CSS': document.querySelector('link[href*="bootstrap"]') !== null,
        'Font Awesome': document.querySelector('link[href*="font-awesome"]') !== null,
        'Sidebar': document.querySelector('.sidebar') !== null,
        'Main Content': document.querySelector('main') !== null
    };

    Object.entries(checks).forEach(([name, passed]) => {
        console.log(`   ${passed ? '✅' : '❌'} ${name}`);
    });

    return Object.values(checks).every(Boolean);
}

// Exposer les fonctions pour les tests manuels
window.testAdminAction = testAdminAction;
window.validateAdminPage = validateAdminPage;

console.log('🎯 Tests disponibles: testAdminAction(action, userId), validateAdminPage()');
console.log('📖 Exemple: testAdminAction("toggle-status", 1)');