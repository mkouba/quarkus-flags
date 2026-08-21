import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/button';
import '@vaadin/icon';
import { notifier } from 'notifier';

/**
 * This component shows the flag cache configuration and implementation,
 * and allows the user to invalidate the cache.
 */
export class QwcFlagCache extends LitElement {

    jsonRpc = new JsonRpc(this);

    static styles = css`
       :host {
            display: flex;
            flex-direction: column;
            gap: 15px;
            height: 100%;
            padding: 10px;
        }
        .cache-info {
            display: grid;
            grid-template-columns: max-content 1fr;
            gap: 5px 20px;
            align-items: center;
        }
        .cache-info dt {
            font-weight: bold;
            color: var(--lumo-secondary-text-color);
        }
        .cache-info dd {
            margin: 0;
        }
        .toolbar {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .status-on {
            color: var(--lumo-success-text-color);
        }
        .status-off {
            color: var(--lumo-error-text-color);
        }
        `;

    static properties = {
         _cache: {state: true},
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this._loadCacheData();
    }

    _loadCacheData() {
        this.jsonRpc.getCacheData()
                    .then(jsonResponse => {
                        this._cache = jsonResponse.result;
                    });
    }

    render() {
        if (this._cache){
            return this._renderCache();
        } else {
            return html`<span>Loading flag cache information...</span>`;
        }
    }

    _renderBoolean(value) {
        return value
            ? html`<vaadin-icon class="status-on" icon="font-awesome-solid:circle-check" title="enabled"></vaadin-icon>`
            : html`<vaadin-icon class="status-off" icon="font-awesome-solid:circle-xmark" title="disabled"></vaadin-icon>`;
    }

    _renderCache(){
        return html`
            <dl class="cache-info">
                <dt>Global cache:</dt>
                <dd>${this._renderBoolean(this._cache.enabled)}</dd>
                <dt>Default TTL:</dt>
                <dd><code>${this._cache.defaultTtl}</code></dd>
                <dt>Implementation:</dt>
                <dd>${this._cache.enabled
                        ? html`<code>${this._cache.implementationClass}</code>`
                        : html`<span class="status-off">N/A</span>`}</dd>
            </dl>

            <div class="toolbar">
                <vaadin-button
                    theme="primary"
                    ?disabled="${!this._cache.enabled}"
                    @click=${this._invalidateAll}>
                    <vaadin-icon icon="font-awesome-solid:trash" slot="prefix"></vaadin-icon>
                    Invalidate all
                </vaadin-button>
            </div>

            ${this._renderProviders()}
        `;
    }

    _renderProviders(){
        return html`
            <vaadin-grid
                .items="${this._cache.providers}"
                class="cache-table"
                theme="no-border">
                <vaadin-grid-sort-column
                    path="id"
                    auto-width
                    header="Provider"
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-column
                    header="Caching"
                    auto-width
                    ${columnBodyRenderer(this._renderCachingEnabled, [])}
                    resizable>
                </vaadin-grid-column>
                <vaadin-grid-sort-column
                    path="ttl"
                    auto-width
                    header="TTL"
                    ${columnBodyRenderer(this._renderTtl, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-column
                    header="Actions"
                    auto-width
                    ${columnBodyRenderer(this._renderProviderActions, [])}
                    resizable>
                </vaadin-grid-column>
            </vaadin-grid>
        `;
    }

    _renderCachingEnabled = (provider) => {
        return this._renderBoolean(provider.cachingEnabled);
    }

    _renderTtl(provider) {
        return html`<code>${provider.ttl}</code>`;
    }

    _renderProviderActions = (provider) => {
        return html`
                 <vaadin-button
                    theme="small"
                    ?disabled="${!provider.cachingEnabled}"
                    @click=${() => this._invalidateProvider(provider)}>
                    Invalidate
                  </vaadin-button>`;
    }

    _invalidateAll() {
        this.jsonRpc.invalidateCache().then(jsonRpcResponse => {
            if (jsonRpcResponse.result) {
                notifier.showInfoMessage("Flag cache invalidated");
            } else {
                notifier.showWarningMessage("Flag cache could not be invalidated");
            }
        });
    }

    _invalidateProvider(provider) {
        this.jsonRpc.invalidateProviderCache({"id": provider.id}).then(jsonRpcResponse => {
            if (jsonRpcResponse.result) {
                notifier.showInfoMessage("Flag cache invalidated for provider '" + provider.id + "'");
            } else {
                notifier.showWarningMessage("Flag cache could not be invalidated for provider '" + provider.id + "'");
            }
        });
    }

}
customElements.define('qwc-cache', QwcFlagCache);
