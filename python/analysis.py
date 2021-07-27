# %%
import enum
import io
import itertools as it
import json
import pathlib
import sys
from IPython.display import display
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
# %%
filename = "log_tournament_newest.json"
# filename = "log_tournament_xx_xx_xxxx_xx_xx.json" # Something else
curr_dir = pathlib.Path(__file__)
log_dir = curr_dir.parent.parent / "logs"
file_to_analyse = log_dir / filename
assert file_to_analyse.exists(), f"File {file_to_analyse} does not exist"
display(f"Found {file_to_analyse} !!!")
display(f"Start loading file...")
# %%
jsonFile = json.load(io.open(file_to_analyse))
display(jsonFile)

# %%
all_results = jsonFile.get("results")
df = pd.json_normalize(all_results)
df["agreed"] = df.utility == 0.0
df["vs"] = None
df["vs_utility"] = None
for i in df["session"].unique():
    df_subset = df[df["session"] == i]
    party1, party2 = df_subset["party"]
    util1, util2 = df_subset["utility"]
    df.loc[(df.party == party1) & (df.session == i), "vs"] = party2
    df.loc[(df.party == party2) & (df.session == i), "vs"] = party1
    df.loc[(df.party == party1) & (df.session == i), "vs_utility"] = util2
    df.loc[(df.party == party2) & (df.session == i), "vs_utility"] = util1

df.to_csv(file_to_analyse.parent / "data.csv")
df
# %%
# for idx, grp in df.groupby("party"):
#     # display(idx)
#     # display(grp)
#     sns.boxplot(data=grp, x="party", y="util")
fig, ax = plt.subplots(1, 1, figsize=(10, 5))
ax = sns.boxplot(data=df, x="party", y="utility")
for tick in ax.get_xticklabels():
    tick.set_rotation(45)
ax.set_xlabel("Party")
ax.set_ylabel("Utility")
plt.show()
# fig.legend(loc="upper left")
# handles, labels = ax.get_legend_handles_labels()
# ax.legend(handles, labels, loc="best")
# %%
fig, ax = plt.subplots(1, 1, figsize=(10, 10))
df_no_aggreement_sum = df.groupby("party").sum()
unique_agents = df_no_aggreement_sum.index.unique()
num_agents = len(unique_agents)
(ax, ) = df_no_aggreement_sum["utility"].plot.bar(
    subplots=True,
    ax=ax,
)
for tick in ax.get_xticklabels():
    tick.set_rotation(45)
ax.set_ylabel("")
fig.suptitle("Number of non-Agreements")
fig.tight_layout()
plt.show()

# %%
fig, ax = plt.subplots(1, 1, figsize=(10, 10))
df_no_aggreement_counts = df[df["agreed"]].groupby("party").count()
unique_agents = df_no_aggreement_counts.index.unique()
num_agents = len(unique_agents)
(ax, ) = df_no_aggreement_counts["session"].plot.pie(
    subplots=True,
    ax=ax,
    autopct="%.3f%%",
    explode=[0.02] * num_agents,
    labels=unique_agents,
    # pctdistance=0.5,
)
ax.set_ylabel("")
fig.suptitle("Number of non-Agreements")
fig.tight_layout()
plt.show()

# %%
interesting_cols = ["CustomAgent", "Hardliner", "Boulware"]
groups = df[df["party"].isin(interesting_cols)].groupby(["party", "agreed"]).count().reset_index("agreed")
num_pies = len(interesting_cols)
fig, axes = plt.subplots(1, num_pies, figsize=(5 * num_pies, 5))
cnt = 0
for idx, ax in zip(groups.index.unique(), axes):
    grp = groups[groups.index == idx]
    ax.pie(
        data=grp,
        x="session",
        autopct="%.3f%%",
        explode=[0.02] * 2,
        labels="agreed",
    )
    ax.set_xlabel(idx)
    # cnt += 1
fig.suptitle(f"Number of non-Agreements")
fig.tight_layout()
plt.show()