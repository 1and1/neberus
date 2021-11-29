<script>
    import {createEventDispatcher} from "svelte";

    export let params;
    export let name;
    export let type;
    export let baseReference;

    const dispatch = createEventDispatcher();

    function dispatchToggleParam(event) {
        dispatch('toggleParam', this);
    }

    function dispatchUpdateParam(event) {
        dispatch('updateParam', this);
    }

    function dispatchUpdateEnumParam(event) {
        this.selectedEnumValue = selectedEnumValue;
        dispatch('updateEnumParam', this);
    }

    function reference(type, escapedName) {
        return baseReference + type + '_' + escapedName;
    }

    function getPlaceholder(param) {
        if (param.extensions && param.extensions['x-allowed-values']) {
            //FIXME return first valueHint?
        }
        return '';
    }

    let selectedEnumValue;

</script>


{#if params.length > 0}
    {#if name}
        <tr>
            <th colspan="4">{name}</th>
        </tr>
    {/if}
    {#each params as param}
        <tr id="{reference(type, param.extensions['x-name-escaped'])}_controls"
            class="controls-row parameter-highlight {(param.required || !param.deprecated) ? '' : 'deactivated'}"
            data-parameter-highlight-name="{baseReference.replaceAll('_curl_', '')}_param_{param.extensions['x-name-escaped']}"
            onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">
            <th></th>
            <td>
                <div class="form-check">
                    <input class="form-check-input" data-type="{type}" data-name="{param.extensions['x-name-escaped']}"
                           type="checkbox" on:change={dispatchToggleParam} checked={param.required || !param.deprecated}
                           disabled={param.required}/>
                </div>
            </td>
            <td>
                <label class="{param.deprecated ? 'curl-param-deprecated' : ''}">{param.name}</label>
            </td>
            <td>
                {#if param.schema.enum}
                    <select class="form-select custom-select mb-3" bind:value={selectedEnumValue} on:change={dispatchUpdateEnumParam}
                            data-type="{type}_value" data-name="{param.extensions['x-name-escaped']}">
                        {#each param.schema.enum as enumValue}
                            <option value="{enumValue}">{enumValue}</option>
                        {/each}
                    </select>
                    <i class="fa fa-caret-down select-caret" aria-hidden="true"></i>
                {:else}
                    <input data-type="{type}_value" data-name="{param.extensions['x-name-escaped']}" type="text"
                           placeholder="{getPlaceholder(param)}"
                           on:keyup={dispatchUpdateParam}/>
                {/if}
            </td>
        </tr>
    {/each}
{/if}


<style>

    label {
        margin-bottom: 0px;
    }

    .form-check {
        padding-left: 30px;
        padding-right: 10px;
    }

    .controls-row input[type="text"] {
        width: 100%;
    }

    .table-curl tr th {
        width: 150px;
    }

    .table-curl tr td:first-of-type {
        max-width: 21px;
    }

    .table-curl tr td:nth-of-type(2) {
        min-width: 150px;
        padding-right: 10px;
    }

</style>