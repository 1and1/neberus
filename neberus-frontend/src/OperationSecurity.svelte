<script>

    export let operation;
    export let openApi;

</script>

{#if operation.extensions && operation.extensions['x-allowed-roles']}
    <div class="card card-primary card-nested">
        <div class="card-header">
            <span><strong>Allowed Roles</strong></span>
            {#each operation.extensions['x-allowed-roles'] as role}
                <div class="role code">{role}</div>
            {/each}
        </div>
    </div>
{/if}

<style>

    .card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
        /*border-radius: 0rem;*/
    }

    .card-header {
        background-color: #b46a02 !important;
    }

    .role {
        margin-left: 10px;
        display: inline-block;
    }

</style>
