import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/dialog';
import 'qui/qui-alert.js';
import { dialogFooterRenderer, dialogRenderer } from '@vaadin/dialog/lit.js';

/**
 * This component shows the feature flags.
 */
export class QwcFeatureFlags extends LitElement {
    
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
         _flags: {state: true},
         _computedFeature: {state: true},
         _computationResult: {state: true},
    };
    
    constructor() {
        super();
    }
    
    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc.getFlagsData()
                    .then(jsonResponse => {
                        this._flags = jsonResponse.result;
                    }); 
    }
    
    render() {
        if (this._flags){
            return this._renderFlags();
        } else {
            return html`<span>Loading feature flags...</span>`;
        }
    }

    _renderFlags(){
        return html`
            <qui-alert level="success">This view contains all feature flags. A flag from a provider with higher priority takes precedence and overrides flags with the same feature name from providers with lower priority.</qui-alert>
            <vaadin-grid
                .items="${this._flags}"
                class="flags-table"
                theme="no-border">
                <vaadin-grid-sort-column
                    path="feature"
                    auto-width
                    header="Feature"
                    ${columnBodyRenderer(this._renderFeature, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-sort-column
                    path="origin"
                    auto-width
                    header="Origin"
                    ${columnBodyRenderer(this._renderOrigin, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-column
                    header="Metadata"
                    width="35rem"
                    ${columnBodyRenderer(this._renderMetadata, [])}
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
              header-title="Computation result for ${this._computedFeature}"
              .opened="${this._computationResult}"
              @closed="${() => {
                this._computationResult = null;
              }}"
              ${dialogFooterRenderer(
                  () => html`
                    <vaadin-button @click="${this._closeComputationDialog}">
                        Close
                    </vaadin-button>
                  `,
                  []
                )}
              ${dialogRenderer(() => html`<strong>${this._computationResult}</strong>`, [])}
            ></vaadin-dialog>
        `;
    }
    
    _renderActions(flag) {
        return html`
                 <vaadin-button
                    theme="primary"
                    @click=${() => this._computeValue(flag)}>
                    Compute value
                  </vaadin-button>`;
    }
    
    _renderFeature(flag) {
            return html`
                ${flag.feature}
            `;
   }
   
   _renderOrigin(flag) {
               return html`
                   ${flag.origin}
               `;
      }
        
   _renderMetadata(flag) {
           if (flag.metadata && flag.metadata.length > 0) {
                return html`
                                    <vaadin-grid .items="${flag.metadata}" class="flag-meta-table" theme="no-border wrap-cell-content row-stripes compact" all-rows-visible>
                                        <vaadin-grid-column auto-width header="Key" ${columnBodyRenderer(this._renderMetadataKey, [])} resizable>
                                        </vaadin-grid-column>
                                        <vaadin-grid-column auto-width header="Value" ${columnBodyRenderer(this._renderMetadataValue, [])} resizable>
                                        </vaadin-grid-column>
                                    </vaadin-grid>
                                    `;
            } else {
                return html``;
            }
   }
   
   _renderMetadataKey(entry) {
                return html`
                    ${entry.key}
                `;
   }
      
   
   _renderMetadataValue(entry) {
    return html`
                    ${entry.value}
                `;
   }  
   
   _computeValue(flag) {
        this._computedFeature = flag.feature;
        this.jsonRpc.computeValue({"feature": flag.feature}).then(jsonRpcResponse => {
            this._computationResult = jsonRpcResponse.result;
        });
   }
   
   _closeComputationDialog() {
       this._computationResult = null;
   }
   
    
}
customElements.define('qwc-flags', QwcFeatureFlags);
