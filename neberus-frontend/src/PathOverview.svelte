<script>
    export let paths;

    function findMaxLength(paths) {
        let longestFound = 0;
        Object.keys(paths).forEach(path => {
            Object.keys(paths[path]).forEach(method => {
                if (method.length > longestFound) {
                    longestFound = method.length;
                }
            })
        })
        return longestFound;
    }

    function padRight(value, length) {
        let padded = value;

        while (padded.length < length) {
            padded += ' ';
        }

        return padded;
    }

    const selectOperation = (event) => {
        let selected = jQuery(event.target).parents("[data-operation]:first").data('operation');

        selectOperationAndScrollTo(selected);
    }

    $: padTo = findMaxLength(paths);

    const initTooltip = el => {
        initTooltipBox(el);
    }

</script>

{#if paths}
    <div class="card card-primary">
        <div class="card-body path-toc">
            <ul class="path-toc-list">

                {#each Object.keys(paths).sort() as path}
                    {#each Object.keys(paths[path]) as method}
                        <li class="{paths[path][method].deprecated?'path-toc-deprecated':''} path-toc-item"
                            data-operation="{method.toUpperCase()}-{paths[path][method].summary.replaceAll(' ', '_')}"
                            on:click={selectOperation}>
                            <div>
                                <span location="" href="" data-container="body" data-toggle="tooltip" use:initTooltip data-placement="left" title=""
                                      data-original-title="{paths[path][method].summary}">
                                    <span class="path-toc-method">{padRight(method.toUpperCase(), padTo)} -</span>
                                    <span class="path-toc-path">{@html path.replaceAll("/", "/<span class='word-wrap'></span>")}</span>
                                </span>
                            </div>
                        </li>
                    {/each}
                {/each}

            </ul>
        </div>
    </div>
{/if}


<style>
    .path-toc-item {
        cursor: pointer;
    }

    .path-toc {
        font-family: monospace;
        font-size: 0.9rem;
        padding-bottom: 5px;
    }

    .path-toc li:hover .path-toc-path {
        text-decoration: underline;
    }

    .path-toc-deprecated {
        text-decoration: line-through;
    }

    .path-toc-list {
        list-style: none;
        margin-left: -40px;
        margin-bottom: 0;
    }

    .path-toc-method {
        float: left;
        white-space: pre;
        padding-right: 8px;
    }

    .path-toc-path {
        word-break: keep-all;
    }

    /* FIXME does not wrap in chrome */
    .word-wrap:after {
        content: "\200b";
    }

</style>