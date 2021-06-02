<script>
    export let param;

    const initTooltip = el => {
        initTooltipBox(el);
    }
</script>

{#if param.pattern}
    <span class="valueHint noselect">@Pattern("{param.pattern}")</span>
{/if}

{#if param.minimum || param.minimum === 0}
    <span class="valueHint noselect">@Min({param.minimum})</span>
{/if}

{#if param.maximum}
    <span class="valueHint noselect">@Max({param.maximum})</span>
{/if}

{#if (param.minItems || param.minItems === 0) && (param.maxItems || param.maxItems === 0)}
    <span class="valueHint noselect">@Size(min={param.minItems}, max={param.maxItems})</span>
{:else if (param.minItems || param.minItems === 0)}
    <span class="valueHint noselect">@Size(min={param.minItems})</span>
{:else if (param.maxItems || param.maxItems === 0)}
    <span class="valueHint noselect">@Size(max={param.maxItems})</span>
{/if}

{#if (param.minLength || param.minLength === 0) && (param.maxLength || param.maxLength === 0)}
    <span class="valueHint noselect">@Size(min={param.minLength}, max={param.maxLength})</span>
{:else if (param.minLength || param.minLength === 0)}
    <span class="valueHint noselect">@Size(min={param.minLength})</span>
{:else if (param.maxLength || param.maxLength === 0)}
    <span class="valueHint noselect">@Size(max={param.maxLength})</span>
{/if}

{#if param.enum}
    <span>
        {param.enum.toString().replaceAll(',', ' | ')}
    </span>
{:else if param.extensions && param.extensions['x-allowed-values']}
    {#each param.extensions['x-allowed-values'] as allowedValue}
        <span class="allowed-value">
        {#if allowedValue.value && allowedValue.valueHint}
            <span data-container="body" data-toggle="tooltip" use:initTooltip data-placement="top"
                  title="{allowedValue.valueHint}" data-original-title="">
                {allowedValue.value}
            </span>
        {:else if allowedValue.valueHint}
            <span class="valueHint noselect">{@html allowedValue.valueHint}</span>
        {:else}
            <span>{allowedValue.value}</span>
        {/if}
        </span>

    {/each}
{/if}

<style>

    .allowed-value {
        word-wrap: anywhere;
    }

    .allowed-value:after {
        content: ' | ';
    }

    .allowed-value:last-of-type:after {
        content: '';
    }

</style>