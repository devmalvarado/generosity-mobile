<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Need extends Model
{
    public $incrementing = false;

    protected $keyType = 'string';

    protected $guarded = [];

    protected $casts = ['details' => 'array', 'expires_on' => 'date:Y-m-d'];

    public function publicData(): array
    {
        return array_merge($this->details, [
            'id' => $this->id, 'centerId' => $this->center_id,
            'status' => $this->status, 'version' => $this->version,
            'quantityRequired' => $this->quantity_required,
            'quantityCommitted' => $this->quantity_committed,
            'expiresOnIsoDate' => $this->expires_on?->toDateString(),
            'updatedAt' => $this->updated_at->toISOString(),
        ]);
    }
}
