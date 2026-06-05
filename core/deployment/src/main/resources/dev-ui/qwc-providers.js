import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';
import { providerOrdering } from 'build-time-data';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/text-field';
import '@vaadin/dialog';
import { dialogFooterRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';

/**
 * This component shows the flag providers.
 */
export class QwcFlagProviders extends LitElement {
    
    jsonRpc = new JsonRpc(this);

    static styles = css`
       :host {
            display: flex;
            flex-direction: column;
            gap: 10px;
            height: 100%;
        }
        `;

    static properties = {
         _providers: {state: true},
         _providerFlags: {state: true},
         _providerId: {state: true},
    };
    
    constructor() {
        super();
    }
    
    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc.getFlagProvidersData()
                    .then(jsonResponse => {
                        this._providers = jsonResponse.result;
                    }); 
    }
    
    render() {
        if (this._providers){
            return this._renderProviders();
        } else {
            return html`<span>Loading flag providers...</span>`;
        }
    }

    _renderProviders(){
        return html`
            <vaadin-grid
                .items="${this._providers}"
                class="flags-table"
                theme="no-border">
                <vaadin-grid-sort-column
                    path="id"
                    auto-width
                    header="Id"
                    ${columnBodyRenderer(this._renderId, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-column
                    auto-width
                    header="Before"
                    ${columnBodyRenderer(this._renderBefore, [])}
                    resizable>
                </vaadin-grid-column>
                <vaadin-grid-column
                    auto-width
                    header="After"
                    ${columnBodyRenderer(this._renderAfter, [])}
                    resizable>
                </vaadin-grid-column>
                <vaadin-grid-column
                    header="Actions"
                    auto-width
                    ${columnBodyRenderer(this._renderActions, [])}
                    resizable>
                </vaadin-grid-column>
            </vaadin-grid>
            
            <vaadin-dialog
               header-title="Flags for ${this._providerId}"
               .opened="${this._providerFlags}"
               @closed="${() => {
                   this._providerFlags = null;
               }}"
               ${dialogFooterRenderer(
                   () => html`
                      <vaadin-button @click="${this._closeListDialog}">
                         Close
                      </vaadin-button>
                     `,
                      []
               )}
               ${dialogRenderer(this._renderProviderFlags, [])}
               ></vaadin-dialog>
        `;
    }
    
    _renderProviderFlags() {
        return this._providerFlags
            ? html`<ul>
                    ${this._providerFlags.map(f =>
                        html`<li><strong>${f}</strong></li>`
                    )}
                   </ul>`
            : html``;
    }
    
    _renderActions(provider) {
        return html`
                 <vaadin-button
                    theme="primary"
                    @click=${() => this._listFlags(provider)}>
                    List flags
                  </vaadin-button>`;
    }
    
    _renderId(provider) {
            return html`
                ${provider.id}
            `;
   }
   
   _renderBefore(provider) {
        const ordering = providerOrdering?.[provider.id];
        const before = ordering?.before;
        return before && before.length > 0 ? html`${before.join(', ')}` : html``;
   }

   _renderAfter(provider) {
        const ordering = providerOrdering?.[provider.id];
        const after = ordering?.after;
        return after && after.length > 0 ? html`${after.join(', ')}` : html``;
   }
        
   _listFlags(provider) {
        this._providerId = provider.id;
        this.jsonRpc.getProviderFlags({"id": provider.id}).then(jsonRpcResponse => {
            this._providerFlags = jsonRpcResponse.result;
        });
   }
   
   _closeListDialog() {
       this._providerFlags = null;
   }
    
}
customElements.define('qwc-providers', QwcFlagProviders);
