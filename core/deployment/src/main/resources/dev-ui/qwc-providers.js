import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';
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
         _providerClassName: {state: true},
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
                    path="className"
                    auto-width
                    header="Class name"
                    ${columnBodyRenderer(this._renderClassName, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-sort-column
                    path="priority"
                    auto-width
                    header="Priority"
                    ${columnBodyRenderer(this._renderPriority, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-column
                    header="Actions"
                    auto-width
                    ${columnBodyRenderer(this._renderActions, [])}
                    resizable>
                </vaadin-grid-column>
            </vaadin-grid>
            
            <vaadin-dialog
               header-title="Flags for ${this._providerClassName}"
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
    
    _renderClassName(provider) {
            return html`
                ${provider.className}
            `;
   }
   
   _renderPriority(provider) {
               return html`
                   ${provider.priority}
               `;
      }
        
   _listFlags(provider) {
        this._providerClassName = provider.className;
        this.jsonRpc.getProviderFlags({"id": provider.id}).then(jsonRpcResponse => {
            this._providerFlags = jsonRpcResponse.result;
        });
   }
   
   _closeListDialog() {
       this._providerFlags = null;
   }
    
}
customElements.define('qwc-providers', QwcFlagProviders);
