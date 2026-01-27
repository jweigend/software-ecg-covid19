package de.qaware.ekg.awb.explorer.ui;

import de.qaware.ekg.awb.common.ui.explorer.api.TreeIconsProvider;
import de.qaware.ekg.awb.sdk.awbapi.explorer.ImporterTreeIconProvider;
import de.qaware.ekg.awb.sdk.awbapi.explorer.TreeIconProviderPluginRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry that hold 0-n importer specific {@link TreeIconsProvider} implementations
 * registered by the importers itself customize the explorer tree view with domain specific icons.
 *
 * This provider overs also a default provider that have to be registered from outside and will
 * given if no matching importer specific provider exits.
 */
@Singleton
public class TreeIconsProviderRegistry implements TreeIconProviderPluginRegistry {

    private Map<String, TreeIconsProvider> providers = new HashMap<>();

    private TreeIconsProvider defaultIconsProvider = null;

    /**
     * Registers a domain specific {@link TreeIconsProvider} instance using the given key
     * and provider instance.
     *
     * @param providerKey the key that identifies the provider
     * @param provider the domain specific provider
     */
    public void registerTreeIconsProvider(String providerKey, TreeIconsProvider provider) {

        if (StringUtils.isBlank(providerKey) || provider == null) {
            throw new IllegalArgumentException("The providerKey or the provider is blank/null.");
        }

        if (providers.containsKey(providerKey)) {
            throw new IllegalStateException("A provider with key '" + providerKey + "' is already registered");
        }

        providers.put(providerKey, provider);
    }

    /**
     * Registers the default {@link TreeIconsProvider} instance that will returned than
     * no other provider matches to a the provider key (see getTreeIconsProvider(String) method)
     *
     * @param provider provider implementation that will registered as default
     */
    public void registerDefaultTreeIconProvider(TreeIconsProvider provider) {
        this.defaultIconsProvider = provider;
    }

    /**
     * Returns the {@link TreeIconsProvider} instance that was registered for the given
     * key before. If no matching provider exists the default provider will returned or
     * null if it wasn't registered.
     *
     * @param providerKey the key of the provider the caller is looking for
     * @return the matching provider for the key or the default provider that can be null
     */
    public TreeIconsProvider getTreeIconsProvider(String providerKey) {

        TreeIconsProvider provider = providers.get(providerKey);

        if (provider == null) {
            return defaultIconsProvider;
        }

        return provider;
    }


    @Override
    public void registerImportTreeIconProvider(String importModuleI, ImporterTreeIconProvider provider) {

    }
}
