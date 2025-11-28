import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/text-field';

/**
 * This component shows the flag evaluators.
 */
export class QwcFlagEvaluators extends LitElement {
    
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
         _evaluators: {state: true},
    };
    
    constructor() {
        super();
    }
    
    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc.getFlagEvaluatorsData()
                    .then(jsonResponse => {
                        this._evaluators = jsonResponse.result;
                    }); 
    }
    
    render() {
        if (this._evaluators){
            return this._renderEvaluators();
        } else {
            return html`<span>Loading flag evaluators...</span>`;
        }
    }

    _renderEvaluators(){
        return html`
            <vaadin-grid
                .items="${this._evaluators}"
                class="flags-table"
                theme="no-border">
                <vaadin-grid-sort-column
                    path="id"
                    auto-width
                    header="Identifier"
                    ${columnBodyRenderer(this._renderId, [])}
                    resizable>
                </vaadin-grid-sort-column>
                <vaadin-grid-sort-column
                    path="className"
                    auto-width
                    header="Class name"
                    ${columnBodyRenderer(this._renderClassName, [])}
                    resizable>
                </vaadin-grid-sort-column>
            </vaadin-grid>
        `;
    }
    
    _renderClassName(evaluator) {
            return html`
                <code>${evaluator.className}</code>
            `;
   }
   
   _renderId(evaluator) {
               return html`
                   ${evaluator.id}
               `;
      }

}
customElements.define('qwc-evaluators', QwcFlagEvaluators);
